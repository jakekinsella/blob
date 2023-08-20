open! Base
open! Core

open Model
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

module Blob = Server.Store.BlobStore(BlobsMock)(BucketsMock)

let%test_unit "test" =
  [%test_eq: bool] true false
