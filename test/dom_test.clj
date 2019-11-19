(ns dom-test
  (:require [clojure.test :refer :all]
            [rehook.dom :as dom]
            [rehook.util :as util]))

(dom/defui test-component [ctx props $]
  (dom/html $ [:div {:ctx ctx :props props}
               [:div {} "Hello world!"]]))

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

    (is (util/rehook-component? test-component))))