type get_request = {
  bucket: string;
  key: string;
} [@@deriving yojson]

type list_request = {
  bucket: string;
  prefix: string;
} [@@deriving yojson]

type delete_request = {
  bucket: string;
  key: string;
} [@@deriving yojson]
