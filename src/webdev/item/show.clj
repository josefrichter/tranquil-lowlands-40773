(ns webdev.item.show
  (:require [hiccup.page :refer [html5]]
            [hiccup.core :refer [html h]]))

(defn item-page [item]
  (let [i (first item)] ; this would typically be a list containing single map, so we want that map
  (html5 {:lang :en}
         [:head
          [:meta {:name :viewport
                  :content "width=device-width, initial-scale=1.0"}]
          [:link {:href "/bootstrap/css/bootstrap.min.css"
                  :rel :stylesheet}]]
         [:body
          [:div.container
           [:h2 "Item detail"]
           [:div.row
            (h (:name i))]
           [:div.row
            (h (:description i))]
           [:div.row
            (if (:checked i)
              "done"
              "not done yet")]]])))