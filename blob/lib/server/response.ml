open Model

type status_response = {
  message : string;
} [@@deriving yojson]

type list_response = {
  blobs : Blob.Head.Frontend.t list
} [@@deriving yojson]
