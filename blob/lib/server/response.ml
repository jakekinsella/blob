open Model

type status_response = {
  message : string;
} [@@deriving yojson]

type blob_response = {
  blob : Blob.Frontend.t
} [@@deriving yojson]
