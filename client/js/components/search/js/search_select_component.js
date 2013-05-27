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
            "click .remove-search-item": "_handleRemoveSearchItem",
            "change .facts-selector": "_handleFactsSelector",
            "change .events-selector": "_handleEventsSelector"
        },
        /**
         * Handling user click on remove for search-item component
         * @private
         */
        _handleRemoveSearchItem: function() {
            this.options.remove_callback(this.cid);
            this.unbind();
        },
        /**
         * Handles facts selector option select
         * @private
         */
        _handleFactsSelector: function(event) {
            var value = $(event.target).val();
            console.log(value);
        },
        _handleEventsSelector: function(event) {
            var value = $(event.target).val();
            var DO = this.model.getUserEventsDO(value);
            console.log(DO);
            _.extend(DO, {state: "event"});
            this.$el.html(this.template(DO)); // draw template
        },
        render: function() {
            this.$el.html(this.template({state: "start",
                                         events: this.model.getUserEventsList(),
                                         facts: this.model.getUserFactsList()
            }));

            return this;
        }
    });

    return SearchComponent;
});
