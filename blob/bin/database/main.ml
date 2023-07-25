open! Base
open! Core
open Lwt

let migrate () =
  let%lwt _ = Database.Connect.connect () in
  let _ = Stdlib.Printf.printf("Migration complete\n") in
  return ()

let rollback () =
  let%lwt _ = Database.Connect.connect () in
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

