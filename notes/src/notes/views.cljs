(ns notes.views
  (:require
    [re-frame.core :as re-frame]
    [notes.styles :as styles]
    [notes.subs :as subs]
    [notes.events :as events]
    [central :as central]))

(defn sidebar [notes] [:div "sidebar" (map (fn [note] [:p (str "Note: " (:title note))]) notes)])
(defn menu [] [:div "menu"])
(defn editor [] [:div "editor"])

(defn main []
  (do
    (re-frame/dispatch  [::events/list-notes])
    (fn []
      (let [notes (re-frame/subscribe [::subs/notes])]
      [:div (sidebar @notes) (menu) (editor)]))))

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
