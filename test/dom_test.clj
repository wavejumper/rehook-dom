(ns dom-test
  (:require [clojure.test :refer :all]
            [rehook.dom :as dom]
            [rehook.util :as util]
            [rehook.dom.server :as server]))

(dom/defui test-component [ctx props $]
  (dom/html $ [:div {:ctx ctx :props props}
               [:div {} "Hello world!"]]))

(deftest defui-symbol-as-component
  (let [hiccup [:div {} "hello world"]]
    (is (= '(:div {} "hello world")
           (dom/html list hiccup)))))

(deftest defui-macros
  (let [result ((test-component {:my :ctx}
                                list)
                {:props :my-props})]

    (is (= result
           '(:div {:ctx {:my :ctx}, :props {:props :my-props}}
             (:div {} "Hello world!"))))

    (is (util/rehook-component? test-component))))

(deftest anon-ui-macros
  (let [anon-component (dom/ui [ctx props $]
                         (dom/html $
                           [:div {:ctx ctx :props props}
                            [:div {} "Hello world!"]]))
        result ((anon-component {:my :ctx}
                                list)
                {:props :my-props})]

    (is (= result
           '(:div {:ctx {:my :ctx}, :props {:props :my-props}}
             (:div {} "Hello world!"))))

    (is (util/rehook-component? anon-component))))


(dom/defui nested-child-component [ctx _ $]
  (dom/html $ [:div ctx "foo"] ))

(dom/defui nested-conditional-component [ctx _ $]
  (dom/html $ [:div ctx (when true
                          [:div ctx
                           (when true
                             [(dom/ui [ctx _ $]
                                  (dom/html $ [:div ctx "Hello world"]))])
                           "---"
                           [nested-child-component]])]))

(deftest complex-eval-logic
  (is (= [:div {:my :ctx}
          [:div {:my :ctx}
           [:div {:my :ctx}
            "Hello world"]
           "---"
           [:div {:my :ctx}
            "foo"]]]

       (server/bootstrap {:my :ctx} (fn [x _] x) identity nested-conditional-component))))