open! Base
open! Core
open Lwt_result

open Common

module BlobStore (Blobs : Database.BlobStore) (Buckets : Database.BucketStore with type connection = Blobs.connection) = struct
  module Context = struct
    type t = {
      connection : Blobs.connection;
      user_id : string option;
    }
  end

  module Policy = struct
    open Model.Bucket.Policy

    module Context = struct
      type t = {
        connection : Blobs.connection;
        user_id : string option;
        bucket : Model.Bucket.t;
      }
    end

    let principal_matches (context : Context.t) principal = match principal with
      | Principal.All -> true
      | Principal.UserId principal ->
        (match context.user_id with
        | Some user_id -> String.(principal = user_id)
        | None -> false)

    let apply context for_action statement : (bool, Common.Api.Error.Database.t) Lwt_result.t =
      let Statement.({ effect; action; principal }) = statement in
      (if principal_matches context principal && Action.(action = for_action) then
        match effect with
        | Effect.Deny -> fail Common.Api.Error.Database.Unauthorized
        | Effect.Allow -> return true
      else
        return false)

    let guard_for (context : Context.t) for_action =
      let%lwt allowed = Magic.Lwt.flatmap (apply context for_action) context.bucket.policy in
      let is_error = List.exists ~f: Result.is_error allowed in
      let is_allowed = List.exists ~f: (fun x ->
        match x with
        | Ok x -> x
        | Error _ -> false) allowed in
      (if is_error then
        fail Common.Api.Error.Database.Unauthorized
      else if is_allowed then
        return ()
      else
        fail Common.Api.Error.Database.Unauthorized)

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

  let get bucket key context =
    let _ = Dream.log "[Store.Blob.get] bucket: `%s` key: `%s`" bucket key in
    let Context.({ connection; _ }) = context in
    Buckets.by_name bucket connection >>= fun bucket ->
      let _ = Dream.log "[Store.Blob.get] bucket: `%s` key: `%s` - found bucket" bucket.name key in
      Policy.Read.guard Policy.Context.({ connection = context.connection; user_id = context.user_id; bucket = bucket }) >>= (fun _ ->
        let _ = Dream.log "[Store.Blob.get] bucket: `%s` key: `%s` - authorized" bucket.name key in
        Blobs.by_key bucket.name key connection)

  let list bucket prefix context =
    let _ = Dream.log "[Store.Blob.list] bucket: `%s` prefix: `%s`" bucket prefix in
    let Context.({ connection; _ }) = context in
    Buckets.by_name bucket connection >>= fun bucket ->
      let _ = Dream.log "[Store.Blob.list] bucket: `%s` prefix: `%s` - found bucket" bucket.name prefix in
      Policy.List.guard Policy.Context.({ connection = context.connection; user_id = context.user_id; bucket = bucket }) >>= (fun _ ->
        let _ = Dream.log "[Store.Blob.list] bucket: `%s` prefix: `%s` - authorized" bucket.name prefix in
        Blobs.by_prefix bucket.name prefix connection)

  let create (blob : Model.Blob.t) context =
    let _ = Dream.log "[Store.Blob.create] bucket: `%s` key: `%s`" blob.bucket blob.key in
    let Context.({ connection; _ }) = context in
    Buckets.by_name blob.bucket connection >>= fun bucket ->
      let _ = Dream.log "[Store.Blob.create] bucket: `%s` key: `%s` - found bucket" blob.bucket blob.key in
      Policy.Write.guard Policy.Context.({ connection = context.connection; user_id = context.user_id; bucket = bucket }) >>= (fun _ ->
        let _ = Dream.log "[Store.Blob.create] bucket: `%s` key: `%s` - authorized" blob.bucket blob.key in
        Blobs.create blob connection)

  let delete bucket key context =
    let _ = Dream.log "[Store.Blob.delete] bucket: `%s` key: `%s`" bucket key in
    let Context.({ connection; _ }) = context in
    Buckets.by_name bucket connection >>= fun bucket ->
      let _ = Dream.log "[Store.Blob.delete] bucket: `%s` key: `%s` - found bucket" bucket.name key in
      Policy.Write.guard Policy.Context.({ connection = context.connection; user_id = context.user_id; bucket = bucket }) >>= (fun _ ->
        let _ = Dream.log "[Store.Blob.delete] bucket: `%s` key: `%s` - authorized" bucket.name key in
        Blobs.delete bucket.name key connection)
end

module Blob = BlobStore(Database.BlobsTable)(Database.BucketsTable)
