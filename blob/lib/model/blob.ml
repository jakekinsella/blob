open! Base
open! Core

module Tag = struct
  module Encoding = struct
    type t = PlainText | Base64 | Other of string [@@deriving yojson]

    let try_make key value =
      if String.(key = "_type") then
        (match value with
        | "plaintext" ->  Some PlainText
        | "base64" ->  Some Base64
        | _type -> Some (Other _type))
      else None
  end

  module Other = struct
    type t = {
      key : string;
      value : string;
    } [@@deriving yojson]

    let make key value = { key = key; value = value }
  end

  type t =
  | Encoding of Encoding.t
  | Other of Other.t
  [@@deriving yojson]


  let make key value =
    Encoding.try_make key value |> Option.map ~f: (fun x -> Encoding x) |>
    Option.value ~default: (Other (Other.make key value))

  let key tag = match tag with
    | Encoding _ -> "_type"
    | Other tag -> tag.key

  let value tag = match tag with
    | Encoding Encoding.PlainText -> "plaintext"
    | Encoding Encoding.Base64 -> "base64"
    | Encoding (Encoding.Other _type) -> _type
    | Other tag -> tag.value

  let to_string tag =
    key tag ^ "=" ^ value tag

  module Frontend = struct
    type t = {
      key : string;
      value : string;
    } [@@deriving yojson]

    let to_frontend tag =
      { key = key tag; value = value tag }
  end
end

type t = {
  bucket : string;
  key : string;
  body : string;
  tags : Tag.t list;
} [@@deriving yojson]

module Frontend = struct
  type outer = t

  type t = {
    bucket : string;
    key : string;
    body : string;
    tags : Tag.Frontend.t list;
  } [@@deriving yojson]

  let to_frontend (blob : outer) =
    { bucket = blob.bucket; key = blob.key; body = blob.body; tags = List.map ~f: Tag.Frontend.to_frontend blob.tags }
end

let from_frontend (blob : Frontend.t) =
  { bucket = blob.bucket; key = blob.key; body = blob.body; tags = List.map ~f: (fun tag -> Tag.make tag.key tag.value ) blob.tags }
