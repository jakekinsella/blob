open Lwt_result

open Common

exception Unauthorized

module Context = struct
  type t = {
    connection : Caqti_lwt.connection;
    user_id : string;
  }
end

module Policy = struct
  open Model.Bucket.Policy

  module Context = struct
    type t = {
      connection : Caqti_lwt.connection;
      user_id : string;
      bucket : Model.Bucket.t;
    }
  end

  let principal_matches (context : Context.t) principal = match principal with
    | Principal.All -> true
    | Principal.UserId principal -> principal == context.user_id

  let apply context for_action statement =
    let Statement.({ effect; action; principal }) = statement in
    (if principal_matches context principal && action == for_action then
      match effect with
      | (Effect.Deny) -> fail Unauthorized
      | (Effect.Allow) -> return ()
    else
      return ())

  let guard_for (context : Context.t) for_action =
    let { head; rest } = context.bucket.policy in
    let%lwt _ = apply context for_action head in
    let%lwt _ = Magic.Lwt.flatmap (apply context for_action) rest in
    return ()

  module Read = struct
    let guard context = guard_for context Action.Read
  end

  module List = struct
    let guard context = guard_for context Action.List
  end

  module Write = struct
    let guard context = guard_for context Action.Write
  end
end

module Blob = struct
  let get bucket key context =
    let Context.({ connection; _ }) = context in
    Database.Buckets.by_name bucket connection >>= fun bucket ->
      let%lwt _ = Policy.Read.guard Policy.Context.({ connection = context.connection; user_id = context.user_id; bucket = bucket }) in
      Database.Blobs.by_key bucket.name key connection

  let list bucket prefix context =
    let Context.({ connection; _ }) = context in
    Database.Buckets.by_name bucket connection >>= fun bucket ->
      let%lwt _ = Policy.List.guard Policy.Context.({ connection = context.connection; user_id = context.user_id; bucket = bucket }) in
      Database.Blobs.by_prefix bucket.name prefix connection

  let create (blob : Model.Blob.t) context =
    let Context.({ connection; _ }) = context in
    Database.Buckets.by_name blob.bucket connection >>= fun bucket ->
      let%lwt _ = Policy.Write.guard Policy.Context.({ connection = context.connection; user_id = context.user_id; bucket = bucket }) in
      Database.Blobs.create blob connection

  let delete bucket key context =
    let Context.({ connection; _ }) = context in
    Database.Buckets.by_name bucket connection >>= fun bucket ->
      let%lwt _ = Policy.Write.guard Policy.Context.({ connection = context.connection; user_id = context.user_id; bucket = bucket }) in
      Database.Blobs.delete bucket.name key connection
end
