open! Base
open! Core

open Common
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
