open! Base
open! Core

open Model
open Common.Api

type map = (string, (Bucket.t, Blob.t) Either.t) Hashtbl.t

module BucketsMock = struct
  type connection = map

  let migrate _ = Lwt.return ()
  let rollback _ = Lwt.return ()

  let by_name _ _ = Lwt_result.fail Error.Database.NotFound
  let create _ _ = Lwt_result.return ()
  let delete _ _ = Lwt_result.return ()
end

module BlobsMock = struct
  type connection = map

  let migrate _ = Lwt.return ()
  let rollback _ = Lwt.return ()

  let by_key _ _ _ = Lwt_result.fail Error.Database.NotFound
  let by_prefix _ _ _ = Lwt_result.fail Error.Database.NotFound
  let create _ _ = Lwt_result.return ()
  let delete _ _ _ = Lwt_result.return ()
end

module Blob = Server.Store.BlobStore(BlobsMock)(BucketsMock)

let%test_unit "test" =
  [%test_eq: bool] true false
