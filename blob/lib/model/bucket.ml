open! Base
open! Core

module Policy = struct
  module Effect = struct
    type t = Allow | Deny [@@deriving yojson]
  end

  module Action = struct
    type t = All | Read | Write | List [@@deriving equal, yojson]

    let (=) action1 action2 =
      ([%equal: t] action1 action2) || ([%equal: t] action1 All) || ([%equal: t] action2 All)
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

  type t = Statement.t list [@@deriving yojson]

  let deny_all = [Statement.({ effect = Effect.Deny; action = Action.All; principal = Principal.All })]
end

type t = {
  name : string;
  policy : Policy.t;
} [@@deriving yojson]
