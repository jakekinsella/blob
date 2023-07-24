open! Base
open! Core

module type t = Caqti_lwt.CONNECTION

let url =
  let user = Sys.getenv "PGUSER" |> Option.value ~default: "UNKNOWN" in
  let password = Sys.getenv "PGPASSWORD" |> Option.value ~default: "UNKNOWN" in
  let host = Sys.getenv "PGHOST" |> Option.value ~default: "UNKNOWN" in
  let port = Sys.getenv "PGPORT" |> Option.value ~default: "UNKNOWN" in
  let database = Sys.getenv "PGDATABASE" |> Option.value ~default: "UNKNOWN" in
    "postgresql://" ^ user ^ ":" ^ password ^ "@" ^ host ^ ":" ^ port ^ "/" ^ database

let connect () =
  match%lwt Caqti_lwt.connect (Uri.of_string url) with
    | Ok connection -> Lwt.return connection
    | Error err -> failwith (Caqti_error.show err)
