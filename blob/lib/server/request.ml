open Model

type get_request = {
  key: string;
} [@@deriving yojson]

type list_request = {
  prefix: string;
} [@@deriving yojson]

type put_request = {
  blob : Blob.Frontend.t
} [@@deriving yojson]

type delete_request = {
  key: string;
} [@@deriving yojson]
