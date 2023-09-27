open! Base
open! Core

open Common
open Common.Api
open Model

module type BlobStore = sig
  type connection

  val migrate : connection -> Unit.t Lwt.t
  val rollback : connection -> Unit.t Lwt.t

  val by_key : string -> string -> connection -> (Blob.t, Error.Database.t) Lwt_result.t
  val by_prefix : string -> string -> connection -> (Blob.Head.t list, Error.Database.t) Lwt_result.t
  val create : Blob.t -> connection -> (Unit.t, Error.Database.t) Lwt_result.t
  val delete : string -> string -> connection -> (Unit.t, Error.Database.t) Lwt_result.t
end

module BlobsTable = struct
  type connection = Caqti_lwt.connection

  let make_tags tags =
    let pairs = String.split ~on: ',' tags in
    let out_tags = pairs
      |> List.map ~f: (String.split ~on: '=')
      |> List.map ~f: (fun pair -> match pair with
        | [key; value] -> Some (Blob.Tag.make key value)
        | _ -> None)
      |> Magic.List.flatten_option in
    out_tags

  let make ~bucket ~key ~body ~tags =
    Blob.({ bucket = bucket; key = key; body = body; tags = make_tags tags })

  let make_head ~bucket ~key ~tags =
    Blob.Head.({ bucket = bucket; key = key; tags = make_tags tags })

  let migrate_query = [%rapper
    execute {sql|
      CREATE TABLE blobs (
        bucket TEXT NOT NULL,
        key TEXT NOT NULL,
        body TEXT NOT NULL,
        tags TEXT NOT NULL,
        FOREIGN KEY(bucket) REFERENCES buckets(name) ON DELETE CASCADE,
        PRIMARY KEY(bucket, key)
      )
    |sql}
  ]

  let rollback_query = [%rapper
    execute {sql|
      DROP TABLE blobs
    |sql}
  ]

  let create_query = [%rapper
    execute {sql|
      INSERT INTO blobs (bucket, key, body, tags)
      VALUES (%string{bucket}, %string{key}, %string{body}, %string{tags})
      ON CONFLICT (bucket, key)
      DO UPDATE SET key=excluded.key, body=excluded.body, tags=excluded.tags
    |sql}
  ]

  let delete_query = [%rapper
    execute {sql|
      DELETE FROM blobs
      WHERE bucket = %string{bucket} AND key = %string{key}
    |sql}
  ]

  let by_key_query = [%rapper
    get_opt {sql|
      SELECT @string{blobs.bucket}, @string{blobs.key}, @string{blobs.body}, @string{blobs.tags}
      FROM blobs
      WHERE bucket = %string{bucket} AND key = %string{key}
    |sql}
    function_out
  ](make)

  let by_prefix_query = [%rapper
    get_many {sql|
      SELECT @string{blobs.bucket}, @string{blobs.key}, @string{blobs.tags}
      FROM blobs
      WHERE bucket = %string{bucket} AND key LIKE (%string{prefix} || '%')
    |sql}
    function_out
  ](make_head)

  let migrate connection =
    let query = migrate_query () in
    query connection |> Error.Database.or_print

  let rollback connection =
    let query = rollback_query () in
    query connection |> Error.Database.or_print

  let by_key bucket key connection =
    let query = by_key_query ~bucket: bucket ~key: key in
    query connection |> Error.Database.or_error_opt

  let by_prefix bucket prefix connection =
    let query = by_prefix_query ~bucket: bucket ~prefix: prefix in
    query connection |> Error.Database.or_error

  let create blob connection =
    let Blob.({ bucket; key; body; tags }) = blob in
    let out_tags = tags |> List.map ~f: Blob.Tag.to_string |> String.concat ~sep: "," in
    let query = create_query ~bucket: bucket ~key: key ~body: body ~tags: out_tags in
    query connection |> Error.Database.or_error

  let delete bucket key connection =
    let query = delete_query ~bucket: bucket ~key: key in
    query connection |> Error.Database.or_error
end
