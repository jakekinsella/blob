(ns notes.components.menu
  (:require
    [re-frame.core :as re-frame]
    [spade.core :refer [defclass]]
    [central :as central]
    [notes.routes :as routes]
    [notes.events :as events]
    [notes.subs :as subs]))

(defclass pane-style []
  {:position "fixed"
   :display "flex"
   :align-items "center"
   :background-color "white"
   :width "100%"
   :height "50px"
   :padding-left "10px"
   :padding-right "10px"
   :border-bottom (str "1px solid" central/Constants.colors.black)
   :box-shadow (str "0px 0px 1px" central/Constants.colors.lightBlack)
   :justify-content "space-between"})
(defn pane [children] (into [:div {:class (pane-style)}] children))

(defclass title-style [] {:font-size "20px" :color central/Constants.colors.black})
(defn title [children] (into [:div {:class (title-style)}] children))

(defclass more-style [] {:padding-top "5px" :padding-right "290px"})
(defn more [child] [:div {:class (more-style)} child])

(defclass delete-style []
  {:cursor "pointer"
   :color central/Constants.colors.black}
  [:&:hover {:color central/Constants.colors.red}])
(defn delete [attrs child] [:div (merge-with + attrs {:class (delete-style)}) child])

(defn build []
  (let [selected @(re-frame/subscribe [::subs/selected])]
    (pane [(title [(:title selected)])
           (more (delete {:on-click (fn [] (re-frame/dispatch [::events/delete-note (:title selected) [::events/navigate ::routes/index]]))} [:> central/Icon {:icon "delete" :size "1.25em"}]))])))
