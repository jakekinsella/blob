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
end

type t = {
  key : string;
  body : string;
  tags : Tag.t list;
} [@@deriving yojson]

module Frontend = struct
  type t = {
    key : string;
    body : string;
    tags : string list;
  } [@@deriving yojson]
end
