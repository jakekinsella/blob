open! Base
open! Core

module Policy = struct
  module Effect = struct
    type t = Allow | Deny [@@deriving yojson]
  end

  module Action = struct
    type t = All | Read | Write | List [@@deriving yojson]
  end

  module Principal = struct
    type t = All | UserId of string [@@deriving yojson]
  end

  module Statement = struct
    type t = {
      effect : Effect.t;
      action : Action.t;
      principal : Principal.t;
    } [@@deriving yojson]
  end

  type t = {
    head : Statement.t;
    rest : Statement.t list;
  } [@@deriving yojson]

  let deny_all = {
    head = Statement.({ effect = Effect.Deny; action = Action.All; principal = Principal.All });
    rest = [];
  }
end

type t = {
  name : string;
  policy : Policy.t;
} [@@deriving yojson]
