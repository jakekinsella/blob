open Common.Api

open Response

let routes = [
  Dream.scope "/api/blobs/" [Common.Middleware.cors] [
    Dream.post "/get" (fun _ ->
      json { message = "ok" } status_response_to_yojson
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
