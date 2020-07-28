(ns convex-web.site.markdown
  (:require [convex-web.site.gui :as gui]
            [convex-web.site.backend :as backend]))

(defn make-page-on-push-hook [k]
  (fn [_ _ set-state]
    (set-state assoc :ajax/status :ajax.status/pending)

    (backend/GET-markdown-page
      k
      {:handler
       (fn [contents]
         (set-state assoc
                    :ajax/status :ajax.status/success
                    :contents contents))

       :error-handler
       (fn [error]
         (set-state assoc
                    :ajax/status :ajax.status/error
                    :ajax/error error))})))

(defn Renderer [{:keys [ajax/status contents toc?] :or {toc? true}}]
  [:div.flex.flex-1.mt-4.mx-10
   (case status
     :ajax.status/pending
     [:div.flex.flex-1.items-center.justify-center
      [gui/Spinner]]

     :ajax.status/error
     [:span "Error"]

     :ajax.status/success
     [:<>
      ;; -- Markdown
      [:div.overflow-auto
       {:class "w-2/4"}

       (for [{:keys [name content]} contents]
         ^{:key name}
         [:article.prose.mb-10
          {:id name}
          [gui/Markdown content]])]

      ;; -- On this page
      (when toc?
        [:div.py-10.px-10
         {:class "w-1/4"}
         [:div.flex.flex-col
          [:span.text-xs.text-gray-500.font-bold.uppercase "On this Page"]

          [:ul.list-none.text-sm.mt-4
           (for [{:keys [name]} contents]
             ^{:key name}
             [:li.mb-2
              [:a.text-gray-600.hover:text-gray-900.cursor-pointer
               {:on-click #(gui/scroll-into-view name)}
               name]])]]])]

     [:div])])
