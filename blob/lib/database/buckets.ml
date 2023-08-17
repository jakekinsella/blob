open! Base
open! Core

open Common.Api
open Model

module type BucketStore = sig
  type connection = Caqti_lwt.connection

  val migrate : connection -> Unit.t Lwt.t
  val rollback : connection -> Unit.t Lwt.t

  val by_name : string -> connection -> (Bucket.t, Error.Database.t) Lwt_result.t
  val create : Bucket.t -> connection -> (Unit.t, Error.Database.t) Lwt_result.t
  val delete : string -> connection -> (Unit.t, Error.Database.t) Lwt_result.t
end

module BucketsTable : BucketStore = struct
  type connection = Caqti_lwt.connection

  let make ~name ~policy =
    let out_policy = policy |> Yojson.Safe.from_string |> Bucket.Policy.of_yojson |> Result.ok |> Option.value ~default: Bucket.Policy.deny_all in
    Bucket.({ name = name; policy = out_policy })

  let migrate_query = [%rapper
    execute {sql|
      CREATE TABLE buckets (
        name TEXT NOT NULL UNIQUE PRIMARY KEY,
        policy TEXT NOT NULL
      )
    |sql}
  ]

  let rollback_query = [%rapper
    execute {sql|
      DROP TABLE buckets
    |sql}
  ]

  let create_query = [%rapper
    execute {sql|
      INSERT INTO buckets (name, policy)
      VALUES (%string{name}, %string{policy})
    |sql}
  ]

  let delete_query = [%rapper
    execute {sql|
      DELETE FROM buckets
      WHERE name = %string{name}
    |sql}
  ]

  let by_name_query = [%rapper
    get_opt {sql|
      SELECT @string{buckets.name}, @string{buckets.policy}
      FROM buckets
      WHERE name = %string{name}
    |sql}
    function_out
  ](make)

  let migrate connection =
    let query = migrate_query () in
    query connection |> Error.Database.or_print

  let rollback connection =
    let query = rollback_query () in
    query connection |> Error.Database.or_print

  let by_name name connection =
    let query = by_name_query ~name: name in
    query connection |> Error.Database.or_error_opt

  let create bucket connection =
    let Bucket.({ name; policy }) = bucket in
    let out_policy = policy |> Bucket.Policy.to_yojson |> Yojson.Safe.to_string in
    let query = create_query ~name: name ~policy: out_policy in
    query connection |> Error.Database.or_error

  let delete name connection =
    let query = delete_query ~name: name in
    query connection |> Error.Database.or_error
end
