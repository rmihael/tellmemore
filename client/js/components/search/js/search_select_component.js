/**
 * This is smaller granular part of the search box. It contains a selector
 * that allow user to select one characteristic/user property/other searchable ones.
 * Author: bearz
 */
define([
    'jquery',
    'backbone',
    'underscore',
    'base_view',
    'text!components/search/templates/search_select_template'
], function($, Backbone, _, BaseView, SelectorTemplate) {
    var SearchComponent = BaseView.extend({
        template: _.template(SelectorTemplate),
        className: "search-item row",
        events: {
            "click .remove-search-item": "_handleRemoveSearchItem"
        },
        /**
         * Handling user click on remove for search-item component
         * @private
         */
        _handleRemoveSearchItem: function() {
            this.options.remove_callback(this.cid);
            this.unbind();
        },

        render: function() {
            this.$el.html(this.template());
        }
    });

    return SearchComponent;
});
