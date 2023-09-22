(ns notes.views
  (:require
    [re-frame.core :as re-frame]
    [react :as react]
    [notes.styles :as styles]
    [notes.subs :as subs]
    [notes.events :as events]
    [notes.components.sidebar :as sidebar]
    [notes.components.menu :as menu]
    [notes.components.editor :as editor]
    [notes.components.dialog :as dialog]
    [central :as central]
    [spade.core :refer [defclass]]))

(defclass root-style [] {:display "flex" :width "100%" :height "100%"})
(defn root [children] (into [:div {:class (root-style)}] children))

(defclass main-style [] {:width "100%"})
(defn main [children] (into [:div {:class (main-style)}] children))

(defclass spacer-style [] {:height "50px"})
(defn spacer [] [:div {:class (spacer-style)}])

(defn index [match]
  (defn render-main [selected]
    (if (nil? selected)
      [:div]
      (main [(menu/build selected) 
             (spacer)
             (editor/build selected)])))
  (defn dispatch-selected [title selected]
    (if (nil? title)
        (if (not (nil? selected))
            (re-frame/dispatch [::events/select-note-clear]))
        (if (not (= title (:title selected))) (re-frame/dispatch [::events/select-note title]))))

  [:f> (do (re-frame/dispatch [::events/list-notes])
      (fn []
        (let [title (:title (:path (:parameters match)))
              notes @(re-frame/subscribe [::subs/notes])
              selected @(re-frame/subscribe [::subs/selected])
              dialog @(re-frame/subscribe [::subs/dialog])]
          (do (dispatch-selected title selected)
              (react/useEffect (fn []
                                   (let [listener (fn [event] (if (= (.-key event) "Escape") (re-frame/dispatch [::events/dialog-close])))]
                                     (do (js/document.addEventListener "keydown" listener)
                                         (fn [] (js/document.removeEventListener "keydown" listener))))))
              (react/useEffect (fn []
                                   (let [listener (fn [] (re-frame/dispatch [::events/dialog-close]))]
                                     (do (js/document.addEventListener "click" listener)
                                         (fn [] (js/document.removeEventListener "click" listener))))))

              (root [(sidebar/build notes)
                     (render-main selected)
                     (dialog/build dialog)])))))])

(def to_login (str central/Constants.central.root "/login?redirect=" (js/encodeURIComponent central/Constants.notes.root)))
(defn login []
  [:> central/Redirect {:to to_login}])

(def routes
  [["/"
    {:name ::index
     :view index}]

   ["/login"
    {:name ::login
     :view login}]

   ["/notes/:title"
    {:name ::notes
     :view index
     :parameters {:path {:title string?}}}]])
