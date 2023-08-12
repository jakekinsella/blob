open Lwt

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

  module Read = struct
    let apply context statement =
      let Statement.({ effect; action; principal }) = statement in
      (if principal_matches context principal then
        (match (effect, action) with
        | (_, Action.Write) -> return ()
        | (_, Action.List) -> return ()
        | (Effect.Deny, (Action.All | Action.Read)) -> fail Unauthorized
        | (Effect.Allow, (Action.All | Action.Read)) -> return ())
      else
        return ())

    let guard (context : Context.t) =
      let { head; rest } = context.bucket.policy in
      let%lwt _ = apply context head in
      let%lwt _ = Magic.Lwt.flatmap (apply context) rest in
      return ()
  end
end

module Blob = struct
  let get bucket key context =
    let Context.({ connection; _ }) = context in
    let%lwt resolved = Database.Buckets.by_name bucket connection in
    match resolved with
    | Ok bucket ->
      let%lwt _ = Policy.Read.guard Policy.Context.({ connection = context.connection; user_id = context.user_id; bucket = bucket }) in
      Database.Blobs.by_key bucket.name key connection
    | Error e -> Lwt_result.fail e

  (* let list bucket prefix = () *)
  (* let create blob = () *)
  (* let delete bucket key = () *)
end
