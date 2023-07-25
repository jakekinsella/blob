open! Base
open! Core
open Async

let migrate () =
  let%bind _ = Database.Connect.connect () in
  let _ = Stdlib.Printf.printf("Migration complete\n") in
  return ()

let rollback () =
  let%bind _ = Database.Connect.connect () in
  let _ = Stdlib.Printf.printf("Rollback complete\n") in
  return ()

let run mode = match mode with
  | "migrate" -> migrate ()
  | "rollback" -> rollback ()
  | _ -> Stdlib.Printf.printf("Invalid mode argument\n"); return ()

let () =
  Command.async
    ~summary: "test"
    Command.Param.(
      let%map.Command mode = anon ("mode" %: string) in
      fun () -> run mode
    )
  |> Command_unix.run

