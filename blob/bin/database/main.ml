open! Base
open! Core
open Lwt

open Model.Bucket

let migrate () =
  let%lwt connection = Database.Connect.connect () in

  let email = "jake.kinsella@gmail.com" in
  let user_id = email |> Uuidm.v5 Uuidm.ns_url |> Uuidm.to_string in
  let policy = [Policy.Statement.({
    effect = Policy.Effect.Allow;
    action = Policy.Action.All;
    principal = Policy.Principal.UserId user_id;
  })]  in
  let bucket = { name = email; policy = policy } in

  let%lwt _ = Database.Buckets.migrate connection in
  let%lwt _ = Database.Blobs.migrate connection in

  let%lwt _ = Database.Buckets.create bucket connection in

  let _ = Stdlib.Printf.printf("Migration complete\n") in
  return ()

let rollback () =
  let%lwt connection = Database.Connect.connect () in
  
  let%lwt _ = Database.Blobs.rollback connection in
  let%lwt _ = Database.Buckets.rollback connection in

  let _ = Stdlib.Printf.printf("Rollback complete\n") in
  return ()

let run mode = match mode with
  | "migrate" -> migrate ()
  | "rollback" -> rollback ()
  | _ -> Stdlib.Printf.printf("Invalid mode argument\n"); return ()

let () =
  Command.basic
    ~summary: "test"
    Command.Param.(
      let%map.Command mode = anon ("mode" %: string) in
      fun () -> Lwt_main.run (run mode)
    )
  |> Command_unix.run

