(ns notes.components.sidebar
  (:require
    [reagent.core :as r]
    [re-frame.core :as re-frame]
    [spade.core :refer [defclass]]
    [central :as central]
    [notes.events :as events]
    [notes.subs :as subs]))

(defclass outer-style [] {:width "100%" :display "flex"})
(defn outer [children] (into [:div {:class (outer-style)}] children))

(defclass pane-style [] {:min-width "18vw" :z-index 10})
(defn pane [children] (into [:div {:class (pane-style)}] children))

(defclass pane-inner-style []
  {:min-width "18vw"
   :max-width "18vw"
   :min-height "100%"
   :max-height "100%"
   :position "fixed"
   :overflow-x "hidden"
   :overflow-y "scroll"
   :border-right (str "1px solid" central/Constants.colors.black)
   :background-color "white"
   :box-shadow (str "0px 0px 1px" central/Constants.colors.black)}
  (at-media {:max-width "750px"}
    {:min-width "100%"}))
(defn pane-inner [children] (into [:div {:class (pane-inner-style)}] children))

(defclass container-style [] {:padding-top "14px" :padding-bottom "50px"})
(defn container [children]
  (into [:div {:class (container-style)}] children))

(defclass header-style []
  {:padding-left "20px"
   :padding-top "5px"
   :padding-bottom "5px"
   :text-decoration "none"
   :font-size "18px"
   :color central/Constants.colors.black}
  [:&:hover {:text-decoration "none" :color "black"}]
  [:&:visited {:text-decoration "none"}]
  [:&:active {:text-decoration "none"}]
  (at-media {:max-width "750px"}
    {:font-size "22px"}))
(defn header [attrs children]
  (into [:a (merge-with + attrs {:class (header-style)})] children))

(defclass spacer-style [] {:width "100%" :height "8px"})
(defn spacer []
  [:div {:class (spacer-style)}])

(defclass item-style []
  {:display "block"
   :padding-left "30px"
   :text-decoration "none"
   :font-size "15px"
   :color central/Constants.colors.black
   :cursor "pointer"}
  [:&:hover {:text-decoration "none" :color "black"}]
  [:&:visited {:text-decoration "none"}]
  [:&:active {:text-decoration "none"}]
  (at-media {:max-width "750px"}
    {:font-size "18px" :padding-top "7px"}))
(defn item [attrs children]
  (into [:a (merge-with + attrs {:class (item-style)})]
    children))

(defclass top-style []
  {:display "flex"})
(defn top [children] (into [:div {:class (top-style)}] children))

(defclass right-style []
  {:padding-top "7px"
   :margin-left "auto"
   :margin-right "20px"
   :cursor "pointer"}
   [:&:hover {:color "black"}]
   [:&:active {:color "black"}]
   (at-media {:max-width "750px"}
    {:padding-right "10px"}))
(defn right [attrs children] (into [:div (merge-with + attrs {:class (right-style)})] children))


(defn build [children]
  (defn render-item [note]
    [item {:href (str "/notes/" (js/encodeURIComponent (:title note)))
           :on-click (fn [] (if (<= (-> js/window .-innerWidth) 750) (re-frame/dispatch [::events/sidebar-close])))
           :key (:title note)}
          [(:title note)]])
  (defn render-add []
    (let [dialog {:title "Add Note"
                  :label "Title"
                  :submit "Save"
                  :on-submit (fn [value] (re-frame/dispatch [::events/save-note value "" [::events/dialog-close]]))}]
      (item {:href "#" 
             :on-click (fn [event] 
                         (do (.stopPropagation event) (re-frame/dispatch [::events/dialog-open dialog])))} "+ Add note")))
  (defn render-add-drawing []
    (let [default {:type "drawing" :width (-> js/window .-screen .-width) :height (-> js/window .-screen .-height (* 2)) :lines []}
          dialog {:title "Add Drawing"
                  :label "Title"
                  :submit "Save"
                  :on-submit (fn [value] (re-frame/dispatch [::events/save-note value default [::events/dialog-close]]))}]
      (item {:href "#" 
             :on-click (fn [event] 
                         (do (.stopPropagation event) (re-frame/dispatch [::events/dialog-open dialog])))} "+ Add drawing")))

  (defn render-sidebar []
    (let [notes @(re-frame/subscribe [::subs/notes])]
      (pane
        [(pane-inner
          [(container
            [(top
              [(header {:href "/"} "Notes")
               (right {:on-click (fn [] (re-frame/dispatch [::events/sidebar-close]))} [[:> central/Icon {:icon "arrow_back_ios_new" :size "1em"}]])])
             (spacer)
             [:div (render-add) (render-add-drawing) (spacer) (map render-item notes)]])])])))

  (let [sidebar-open? @(re-frame/subscribe [::subs/sidebar-open?])]
    (into (outer [(if sidebar-open? (render-sidebar) [:div])]) children)))
