(ns notes.views
  (:require
   [re-frame.core :as re-frame]
   [notes.styles :as styles]
   [notes.subs :as subs]
   [notes.events :as events]
   [central :as central]))

(defn main []
  (do
    (re-frame.core/dispatch  [::events/list-notes])
    (fn []
      (let [notes (re-frame/subscribe [::subs/notes])]
      [:div
        [:div "Hello world"]
        (map (fn [note] [:div "test"]) @notes)]))))

(def to_login (str central/Constants.central.root "/login?redirect=" (js/encodeURIComponent central/Constants.notes.root)))
(defn login []
  [:> central/Redirect {:to to_login}])

(def routes
  [["/"
    {:name ::main
     :view main}]

   ["/login"
    {:name ::login
     :view login}]])
