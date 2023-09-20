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

(defclass main-style [] {:width "100%"})
(defn main [children] (into [:div {:class (main-style)}] children))

(defclass spacer-style [] {:height "50px"})
(defn spacer [] [:div {:class (spacer-style)}])

(defn index []
  (do
    (re-frame/dispatch [::events/list-notes])
    (re-frame/dispatch [::events/select-note "Test note"])
    (fn []
      (let [notes @(re-frame/subscribe [::subs/notes])
            selected @(re-frame/subscribe [::subs/selected])]
        (root [(sidebar/build notes)
               (main [(menu/build selected) 
                      (spacer)
                      (editor/build selected)])])))))

(def to_login (str central/Constants.central.root "/login?redirect=" (js/encodeURIComponent central/Constants.notes.root)))
(defn login []
  [:> central/Redirect {:to to_login}])

(def routes
  [["/"
    {:name ::index
     :view index}]

   ["/login"
    {:name ::login
     :view login}]])
