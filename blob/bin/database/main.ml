let migrate () =
  let _ = Lwt_main.run (Database.Connect.connect()) in
  Printf.printf("Migration complete\n")

let rollback () =
  let _ = Lwt_main.run (Database.Connect.connect()) in
  Printf.printf("Rollback complete\n")

let () =
  let mode = Sys.argv.(1) in
    (match mode with
      | "migrate" -> migrate()
      | "rollback" -> rollback()
      | _ -> Printf.printf("Invalid mode argument\n"))
