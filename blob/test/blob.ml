open! Base
open! Core

let%test_unit "test" =
  [%test_eq: bool] true false
