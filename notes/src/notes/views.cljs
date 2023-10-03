(ns notes.views
  (:require
    [re-frame.core :as re-frame]
    [react :as react]
    [notes.routes :as routes]
    [notes.styles :as styles]
    [notes.subs :as subs]
    [notes.events :as events]
    [notes.components.sidebar :as sidebar]
    [notes.components.menu :as menu]
    [notes.components.editor :as editor]
    [notes.components.dialog :as dialog]
    [central :as central]
    [spade.core :refer [defclass defglobal]]))

(defglobal global-styles
  [:#app {:height "100%"}]
  [:#app>div {:height "100%"}])

(defclass root-style [] {:display "flex" :width "100%" :height "100%"})
(defn root [children] (into [:div {:class (root-style)}] children))

(defclass main-style [] {:width "100%" :height "100%"})
(defn main [children] (into [:div {:class (main-style)}] children))

(defn index [match]
  (defn render-main [title]
    [main [[menu/build]
           (if (nil? title) [:div] [editor/build])]])

  [:f> (do (re-frame/dispatch [::events/list-notes])
    (let [title (:title (:path (:parameters match)))]
      (fn []
        (do (re-frame/dispatch [::events/select-note title])
            (react/useEffect (fn []
                                 (let [listener (fn [event] (if (= (.-key event) "Escape") (re-frame/dispatch [::events/dialog-close])))]
                                   (do (js/document.addEventListener "keydown" listener)
                                       (fn [] (js/document.removeEventListener "keydown" listener))))))
            (react/useEffect (fn []
                                 (let [listener (fn [] (re-frame/dispatch [::events/dialog-close]))]
                                   (do (js/document.addEventListener "click" listener)
                                       (fn [] (js/document.removeEventListener "click" listener))))))
            (react/useEffect (fn [] (let [interval (fn [] (re-frame/dispatch [::events/list-notes true]))]
                                      (do (js/setInterval interval 10000) (fn [] (js/clearInterval interval))))))

            [root [[sidebar/build [[render-main title]]]
                   [dialog/build]]]))))])

(def to_login (str central/Constants.central.root "/login?redirect=" (js/encodeURIComponent central/Constants.notes.root)))
(defn login []
  [:> central/Redirect {:to to_login}])

(def routes
  [["/"
    {:name ::routes/index
     :view index}]

   ["/login"
    {:name ::routes/login
     :view login}]

   ["/notes/:title"
    {:name ::routes/notes
     :view index
     :parameters {:path {:title string?}}}]])
