open! Base
open! Core
open Lwt

open Common
open Common.Api
open Request
open Response

let routes = [
  Dream.scope "/api/blobs/" [Common.Middleware.cors; Common.Middleware.Auth.require_auth] [
    Dream.post "/get" (fun request ->
      let user_id = Dream.field request Common.Middleware.Auth.user_id in
      let%lwt connection = Dream.sql request (fun connection -> return connection) in
      let context = Store.Context.({ connection; user_id }) in

      let%lwt body = Dream.body request in
      let req = body |> Yojson.Safe.from_string |> get_request_of_yojson in

      match req with
      | Ok { bucket; key } ->
        Dream.log "[/blobs/get] bucket: `%s` key: `%s`" bucket key;
        (match%lwt Store.Blob.get bucket key context with
        | Ok _ ->
          json { message = "ok" } status_response_to_yojson
        | Error e ->
          Dream.log "[/blobs/get] bucket: `%s` key: `%s` - failed with `%s`" bucket key (Api.Error.Database.to_string e);
          throw_error (Error.Frontend.InternalServerError (Api.Error.Database.to_string e)))
      | Error _ ->
        throw_error Error.Frontend.BadRequest
    );

    Dream.post "/list" (fun _ ->
      json { message = "ok" } status_response_to_yojson
    );

    Dream.post "/put" (fun _ ->
      json { message = "ok" } status_response_to_yojson
    );

    Dream.post "/delete" (fun _ ->
      json { message = "ok" } status_response_to_yojson
    );
  ]
]
