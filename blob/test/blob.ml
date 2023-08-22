open! Base
open! Core

open Model
open Server
open Common.Api

type bucket = {
  bucket : Bucket.t;
  blobs: (string, Blob.t) Hashtbl.t;
}
type table = {
  buckets : (string, bucket) Hashtbl.t;
}

module BucketsMock = struct
  open Model.Bucket

  type connection = table

  let migrate _ = Lwt.return ()
  let rollback _ = Lwt.return ()

  let by_name bucket table =
    match Hashtbl.find table.buckets bucket with
    | Some { bucket; blobs = _ } -> Lwt_result.return bucket
    | None -> Lwt_result.fail Error.Database.NotFound
  
  let create bucket table =
    let _ = Hashtbl.set table.buckets ~key: bucket.name ~data: { bucket = bucket; blobs = Hashtbl.create (module String) } in
    Lwt_result.return ()

  let delete bucket table =
    let _ = Hashtbl.remove table.buckets bucket in
    Lwt_result.return ()
end

module BlobsMock = struct
  open Model.Blob

  type connection = table

  let migrate _ = Lwt.return ()
  let rollback _ = Lwt.return ()

  let by_key bucket key table =
    Hashtbl.find table.buckets bucket |>
      Option.map ~f: (fun { bucket = _; blobs } -> Hashtbl.find blobs key) |>
      Option.join |>
      Option.map ~f: Lwt_result.return |>
      Option.value ~default: (Lwt_result.fail Error.Database.NotFound)

  let by_prefix bucket key table =
    match Hashtbl.find table.buckets bucket with
    | Some { bucket = _; blobs } ->
      Hashtbl.data blobs |>
      List.filter ~f: (fun blob -> String.is_prefix ~prefix: key blob.key) |>
      List.map ~f: (fun blob ->
        let Blob.({ bucket; key; body = _; tags }) = blob in
          Blob.Head.({ bucket; key; tags })) |>
      Lwt_result.return
    | None -> Lwt_result.fail Error.Database.NotFound

  let create blob table =
    match Hashtbl.find table.buckets blob.bucket with
    | Some { bucket = _; blobs } ->
      let _ = Hashtbl.set blobs ~key: blob.key ~data: blob in
      Lwt_result.return ()
    | None -> Lwt_result.fail Error.Database.NotFound

  let delete bucket key table =
    match Hashtbl.find table.buckets bucket with
    | Some { bucket = _; blobs } ->
      let _ = Hashtbl.remove blobs key in
      Lwt_result.return ()
    | None -> Lwt_result.fail Error.Database.NotFound
end

module Blob = Store.BlobStore(BlobsMock)(BucketsMock)

let%test_unit "deny all (create)" =
  let table = { buckets = Hashtbl.create (module String) } in
  let context = Blob.Context.({ connection = table; user_id = Some "user" }) in
  let _ = BucketsMock.create Bucket.({ name = "test"; policy = Bucket.Policy.deny_all }) table |> Lwt_main.run in

  let res = Blob.create Model.Blob.({ bucket = "test"; key = "key"; body = ""; tags = [] }) context |> Lwt_main.run in
  [%test_eq: (unit, Error.Database.t) Result.t] res (Error Error.Database.Unauthorized)

let%test_unit "deny all (delete)" =
  let table = { buckets = Hashtbl.create (module String) } in
  let context = Blob.Context.({ connection = table; user_id = Some "user" }) in
  let _ = BucketsMock.create Bucket.({ name = "test"; policy = Bucket.Policy.deny_all }) table |> Lwt_main.run in
  
  let res = Blob.delete "test" "key" context |> Lwt_main.run in
  [%test_eq: (unit, Error.Database.t) Result.t] res (Error Error.Database.Unauthorized)

let%test_unit "deny all (get)" =
  let table = { buckets = Hashtbl.create (module String) } in
  let context = Blob.Context.({ connection = table; user_id = Some "user" }) in
  let _ = BucketsMock.create Bucket.({ name = "test"; policy = Bucket.Policy.deny_all }) table |> Lwt_main.run in
  
  let res = Blob.get "test" "key" context |> Lwt_main.run in
  [%test_eq: (Model.Blob.t, Error.Database.t) Result.t] res (Error Error.Database.Unauthorized)

let%test_unit "deny all (list)" =
  let table = { buckets = Hashtbl.create (module String) } in
  let context = Blob.Context.({ connection = table; user_id = Some "user" }) in
  let _ = BucketsMock.create Bucket.({ name = "test"; policy = Bucket.Policy.deny_all }) table |> Lwt_main.run in
  
  let res = Blob.list "test" "key" context |> Lwt_main.run in
  [%test_eq: (Model.Blob.Head.t list, Error.Database.t) Result.t] res (Error Error.Database.Unauthorized)


let user_policy = [Bucket.Policy.Statement.({
  effect = Bucket.Policy.Effect.Allow;
  action = Bucket.Policy.Action.All;
  principal = Bucket.Policy.Principal.UserId "user";
})]

let%test_unit "user_id = user, deny (create)" =
  let table = { buckets = Hashtbl.create (module String) } in
  let context = Blob.Context.({ connection = table; user_id = Some "other" }) in
  let _ = BucketsMock.create Bucket.({ name = "test"; policy = user_policy }) table |> Lwt_main.run in

  let res = Blob.create Model.Blob.({ bucket = "test"; key = "key"; body = ""; tags = [] }) context |> Lwt_main.run in
  [%test_eq: (unit, Error.Database.t) Result.t] res (Error Error.Database.Unauthorized)

let%test_unit "user_id = user, deny (delete)" =
  let table = { buckets = Hashtbl.create (module String) } in
  let context = Blob.Context.({ connection = table; user_id = Some "other" }) in
  let _ = BucketsMock.create Bucket.({ name = "test"; policy = user_policy }) table |> Lwt_main.run in
  
  let res = Blob.delete "test" "key" context |> Lwt_main.run in
  [%test_eq: (unit, Error.Database.t) Result.t] res (Error Error.Database.Unauthorized)

let%test_unit "user_id = user, deny (get)" =
  let table = { buckets = Hashtbl.create (module String) } in
  let context = Blob.Context.({ connection = table; user_id = Some "other" }) in
  let _ = BucketsMock.create Bucket.({ name = "test"; policy = user_policy }) table |> Lwt_main.run in
  
  let res = Blob.get "test" "key" context |> Lwt_main.run in
  [%test_eq: (Model.Blob.t, Error.Database.t) Result.t] res (Error Error.Database.Unauthorized)

let%test_unit "user_id = user, deny (list)" =
  let table = { buckets = Hashtbl.create (module String) } in
  let context = Blob.Context.({ connection = table; user_id = Some "other" }) in
  let _ = BucketsMock.create Bucket.({ name = "test"; policy = user_policy }) table |> Lwt_main.run in
  
  let res = Blob.list "test" "key" context |> Lwt_main.run in
  [%test_eq: (Model.Blob.Head.t list, Error.Database.t) Result.t] res (Error Error.Database.Unauthorized)

let%test_unit "user_id = user, allow (create)" =
  let table = { buckets = Hashtbl.create (module String) } in
  let context = Blob.Context.({ connection = table; user_id = Some "user" }) in
  let _ = BucketsMock.create Bucket.({ name = "test"; policy = user_policy }) table |> Lwt_main.run in

  let res = Blob.create Model.Blob.({ bucket = "test"; key = "key"; body = ""; tags = [] }) context |> Lwt_main.run in
  [%test_eq: (unit, Error.Database.t) Result.t] res (Ok ())

let%test_unit "user_id = user, allow (delete)" =
  let table = { buckets = Hashtbl.create (module String) } in
  let context = Blob.Context.({ connection = table; user_id = Some "user" }) in
  let _ = BucketsMock.create Bucket.({ name = "test"; policy = user_policy }) table |> Lwt_main.run in
  
  let res = Blob.delete "test" "key" context |> Lwt_main.run in
  [%test_eq: (unit, Error.Database.t) Result.t] res (Ok ())

let%test_unit "user_id = user, allow (get)" =
  let table = { buckets = Hashtbl.create (module String) } in
  let context = Blob.Context.({ connection = table; user_id = Some "user" }) in
  let _ = BucketsMock.create Bucket.({ name = "test"; policy = user_policy }) table |> Lwt_main.run in
  
  let blob = Model.Blob.({ bucket = "test"; key = "key"; body = ""; tags = [] }) in
  let _ = Blob.create blob context |> Lwt_main.run in
  let res = Blob.get "test" "key" context |> Lwt_main.run in
  [%test_eq: (Model.Blob.t, Error.Database.t) Result.t] res (Ok blob)

let%test_unit "user_id = user, allow (list)" =
  let table = { buckets = Hashtbl.create (module String) } in
  let context = Blob.Context.({ connection = table; user_id = Some "user" }) in
  let _ = BucketsMock.create Bucket.({ name = "test"; policy = user_policy }) table |> Lwt_main.run in
  
  let blob = Model.Blob.({ bucket = "test"; key = "key"; body = ""; tags = [] }) in
  let _ = Blob.create blob context |> Lwt_main.run in
  let res = Blob.list "test" "ke" context |> Lwt_main.run in
  [%test_eq: (Model.Blob.Head.t list, Error.Database.t) Result.t] res (Ok [Model.Blob.Head.({ bucket = "test"; key = "key"; tags = [] })])


let conflicting = [
  Bucket.Policy.Statement.({
    effect = Bucket.Policy.Effect.Allow;
    action = Bucket.Policy.Action.All;
    principal = Bucket.Policy.Principal.UserId "user";
  });
  Bucket.Policy.Statement.({
    effect = Bucket.Policy.Effect.Deny;
    action = Bucket.Policy.Action.All;
    principal = Bucket.Policy.Principal.UserId "user";
  })
]

let%test_unit "conflicting, deny (create)" =
  let table = { buckets = Hashtbl.create (module String) } in
  let context = Blob.Context.({ connection = table; user_id = Some "user" }) in
  let _ = BucketsMock.create Bucket.({ name = "test"; policy = conflicting }) table |> Lwt_main.run in

  let res = Blob.create Model.Blob.({ bucket = "test"; key = "key"; body = ""; tags = [] }) context |> Lwt_main.run in
  [%test_eq: (unit, Error.Database.t) Result.t] res (Error Error.Database.Unauthorized)

let%test_unit "conflicting, deny (delete)" =
  let table = { buckets = Hashtbl.create (module String) } in
  let context = Blob.Context.({ connection = table; user_id = Some "user" }) in
  let _ = BucketsMock.create Bucket.({ name = "test"; policy = conflicting }) table |> Lwt_main.run in
  
  let res = Blob.delete "test" "key" context |> Lwt_main.run in
  [%test_eq: (unit, Error.Database.t) Result.t] res (Error Error.Database.Unauthorized)

let%test_unit "conflicting, deny (get)" =
  let table = { buckets = Hashtbl.create (module String) } in
  let context = Blob.Context.({ connection = table; user_id = Some "user" }) in
  let _ = BucketsMock.create Bucket.({ name = "test"; policy = conflicting }) table |> Lwt_main.run in
  
  let res = Blob.get "test" "key" context |> Lwt_main.run in
  [%test_eq: (Model.Blob.t, Error.Database.t) Result.t] res (Error Error.Database.Unauthorized)

let%test_unit "conflicting, deny (list)" =
  let table = { buckets = Hashtbl.create (module String) } in
  let context = Blob.Context.({ connection = table; user_id = Some "user" }) in
  let _ = BucketsMock.create Bucket.({ name = "test"; policy = conflicting }) table |> Lwt_main.run in
  
  let res = Blob.list "test" "key" context |> Lwt_main.run in
  [%test_eq: (Model.Blob.Head.t list, Error.Database.t) Result.t] res (Error Error.Database.Unauthorized)


let partial_conflicting = [
  Bucket.Policy.Statement.({
    effect = Bucket.Policy.Effect.Allow;
    action = Bucket.Policy.Action.All;
    principal = Bucket.Policy.Principal.UserId "user";
  });
  Bucket.Policy.Statement.({
    effect = Bucket.Policy.Effect.Deny;
    action = Bucket.Policy.Action.Write;
    principal = Bucket.Policy.Principal.UserId "user";
  })
]

let%test_unit "partial conflicting, deny (create)" =
  let table = { buckets = Hashtbl.create (module String) } in
  let context = Blob.Context.({ connection = table; user_id = Some "user" }) in
  let _ = BucketsMock.create Bucket.({ name = "test"; policy = partial_conflicting }) table |> Lwt_main.run in

  let res = Blob.create Model.Blob.({ bucket = "test"; key = "key"; body = ""; tags = [] }) context |> Lwt_main.run in
  [%test_eq: (unit, Error.Database.t) Result.t] res (Error Error.Database.Unauthorized)

let%test_unit "partial conflicting, deny (delete)" =
  let table = { buckets = Hashtbl.create (module String) } in
  let context = Blob.Context.({ connection = table; user_id = Some "user" }) in
  let _ = BucketsMock.create Bucket.({ name = "test"; policy = partial_conflicting }) table |> Lwt_main.run in
  
  let res = Blob.delete "test" "key" context |> Lwt_main.run in
  [%test_eq: (unit, Error.Database.t) Result.t] res (Error Error.Database.Unauthorized)

let%test_unit "partial conflicting, allow (get)" =
  let table = { buckets = Hashtbl.create (module String) } in
  let context = Blob.Context.({ connection = table; user_id = Some "user" }) in
  let _ = BucketsMock.create Bucket.({ name = "test"; policy = partial_conflicting }) table |> Lwt_main.run in
  
  let res = Blob.get "test" "key" context |> Lwt_main.run in
  [%test_eq: (Model.Blob.t, Error.Database.t) Result.t] res (Error Error.Database.NotFound)

let%test_unit "partial conflicting, allow (list)" =
  let table = { buckets = Hashtbl.create (module String) } in
  let context = Blob.Context.({ connection = table; user_id = Some "user" }) in
  let _ = BucketsMock.create Bucket.({ name = "test"; policy = partial_conflicting }) table |> Lwt_main.run in
  
  let res = Blob.list "test" "key" context |> Lwt_main.run in
  [%test_eq: (Model.Blob.Head.t list, Error.Database.t) Result.t] res (Ok [])
