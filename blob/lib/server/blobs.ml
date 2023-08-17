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
      let context = Store.Blob.Context.({ connection; user_id }) in

      let%lwt body = Dream.body request in
      let req = body |> Yojson.Safe.from_string |> get_request_of_yojson in

      match req with
      | Ok { bucket; key } ->
        Dream.log "[/blobs/get] bucket: `%s` key: `%s`" bucket key;
        (match%lwt Store.Blob.get bucket key context with
        | Ok blob ->
          json (blob |> Model.Blob.Frontend.to_frontend) Model.Blob.Frontend.to_yojson
        | Error e ->
          Dream.log "[/blobs/get] bucket: `%s` key: `%s` - failed with `%s`" bucket key (Api.Error.Database.to_string e);
          throw_error (Error.Database.to_frontend e))
      | Error _ ->
        throw_error Error.Frontend.BadRequest
    );

    Dream.post "/list" (fun request ->
      let user_id = Dream.field request Common.Middleware.Auth.user_id in
      let%lwt connection = Dream.sql request (fun connection -> return connection) in
      let context = Store.Blob.Context.({ connection; user_id }) in

      let%lwt body = Dream.body request in
      let req = body |> Yojson.Safe.from_string |> list_request_of_yojson in

      match req with
      | Ok { bucket; prefix } ->
        Dream.log "[/blobs/list] bucket: `%s` prefix: `%s`" bucket prefix;
        (match%lwt Store.Blob.list bucket prefix context with
        | Ok blobs ->
          json { blobs = blobs |> List.map ~f: Model.Blob.Head.Frontend.to_frontend } list_response_to_yojson
        | Error e ->
          Dream.log "[/blobs/list] bucket: `%s` prefix: `%s` - failed with `%s`" bucket prefix (Api.Error.Database.to_string e);
          throw_error (Error.Database.to_frontend e))
      | Error _ ->
        throw_error Error.Frontend.BadRequest
    );

    Dream.post "/create" (fun request ->
      let user_id = Dream.field request Common.Middleware.Auth.user_id in
      let%lwt connection = Dream.sql request (fun connection -> return connection) in
      let context = Store.Blob.Context.({ connection; user_id }) in

      let%lwt body = Dream.body request in
      let blob = body |> Yojson.Safe.from_string |> Model.Blob.Frontend.of_yojson in

      match blob with
      | Ok blob ->
        Dream.log "[/blobs/create] bucket: `%s` key: `%s`" blob.bucket blob.key;
        (match%lwt Store.Blob.create (Model.Blob.from_frontend blob) context with
        | Ok _ ->
          json { message = "ok" } status_response_to_yojson
        | Error e ->
          Dream.log "[/blobs/create] bucket: `%s` key: `%s` - failed with `%s`" blob.bucket blob.key (Api.Error.Database.to_string e);
          throw_error (Error.Database.to_frontend e))
      | Error _ ->
        throw_error Error.Frontend.BadRequest
    );

    Dream.post "/delete" (fun request ->
      let user_id = Dream.field request Common.Middleware.Auth.user_id in
      let%lwt connection = Dream.sql request (fun connection -> return connection) in
      let context = Store.Blob.Context.({ connection; user_id }) in

      let%lwt body = Dream.body request in
      let req = body |> Yojson.Safe.from_string |> delete_request_of_yojson in

      match req with
      | Ok { bucket; key } ->
        Dream.log "[/blobs/delete] bucket: `%s` key: `%s`" bucket key;
        (match%lwt Store.Blob.delete bucket key context with
        | Ok _ ->
          json { message = "ok" } status_response_to_yojson
        | Error e ->
          Dream.log "[/blobs/delete] bucket: `%s` key: `%s` - failed with `%s`" bucket key (Api.Error.Database.to_string e);
          throw_error (Error.Database.to_frontend e))
      | Error _ ->
        throw_error Error.Frontend.BadRequest
    );
  ]
]
