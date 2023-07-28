open! Base
open! Core

open Common
open Common.Api
open Model

let make ~key ~body ~tags =
  let pairs = String.split ~on: ',' tags in
  let out_tags = pairs
    |> List.map ~f: (String.split ~on: '=')
    |> List.map ~f: (fun pair -> match pair with
      | [key; value] -> Some (Blob.Tag.make key value)
      | _ -> None)
    |> Magic.List.flatten_option in
  Blob.({ key = key; body = body; tags = out_tags })

let migrate_query = [%rapper
  execute {sql|
    CREATE TABLE blobs (
      key TEXT NOT NULL UNIQUE PRIMARY KEY,
      body TEXT NOT NULL,
      tags TEXT NOT NULL
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
    INSERT INTO blobs (key, body, tags)
    VALUES (%string{key}, %string{body}, %string{tags})
  |sql}
]

let delete_query = [%rapper
  execute {sql|
    DELETE FROM blobs
    WHERE key = %string{key}
  |sql}
]

let by_key_query = [%rapper
  get_opt {sql|
    SELECT @string{blobs.key}, @string{blobs.body}, @string{blobs.tags}
    FROM blobs
    WHERE key = %string{key}
  |sql}
  function_out
](make)

let by_prefix_query = [%rapper
  get_many {sql|
    SELECT @string{blobs.key}, @string{blobs.body}, @string{blobs.tags}
    FROM blobs
    WHERE key LIKE (%string{prefix} || '%')
  |sql}
  function_out
](make)

let migrate connection =
  let query = migrate_query () in
    query connection |> Error.Database.or_print

let rollback connection =
  let query = rollback_query () in
    query connection |> Error.Database.or_print

let by_key key connection =
  let query = by_key_query ~key: key in
    query connection |> Error.Database.or_error_opt

let by_prefix prefix connection =
  let query = by_prefix_query ~prefix: prefix in
    query connection |> Error.Database.or_error

let create blob connection =
  let Blob.({ key; body; tags }) = blob in
  let out_tags = tags |> List.map ~f: Blob.Tag.to_string |> String.concat ~sep: "," in
  let query = create_query ~key: key ~body: body ~tags: out_tags in
    query connection |> Error.Database.or_error

let delete key connection =
  let query = delete_query ~key: key in
    query connection |> Error.Database.or_error

