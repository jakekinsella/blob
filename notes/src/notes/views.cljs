(ns notes.views
  (:require
    [re-frame.core :as re-frame]
    [notes.styles :as styles]
    [notes.subs :as subs]
    [notes.events :as events]
    [notes.components.sidebar :as sidebar]
    [notes.components.menu :as menu]
    [notes.components.editor :as editor]
    [central :as central]
    [spade.core :refer [defclass]]))

(defclass root-style [] {:display "flex" :width "100%" :height "100%"})
(defn root [children] (into [:div {:class (root-style)}] children))

(defn main []
  (do
    (re-frame/dispatch [::events/list-notes])
    (re-frame/dispatch [::events/select-note "Test note"])
    (fn []
      (let [notes @(re-frame/subscribe [::subs/notes])
            selected @(re-frame/subscribe [::subs/selected])]
        (root [(sidebar/build notes) (menu/build selected) (editor/build selected)])))))

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
