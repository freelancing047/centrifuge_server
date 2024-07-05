/**
 * Represents a dataview definition
 */
define(['underscore', 'backbone', 'app/models/base', 'api/csi.dataview'],
function(_, Backbone, BaseModel, DataviewAPI) {
    return BaseModel.extend({
        initialize: function() {
        },

        /**
         * Load ALL data for this dataview definition
         */
        open: function() {
            console.log("opening dataview (this could take a while...)");
            var self = this;
            // use the API to get the full set of metadata
            DataviewAPI.openDataView(this.id, {
                onsuccess: function(response) {
                    console.log("openDataView response", response);
                    // just use the simple return value, no nested stuff yet
                    self.set(response.resultData);
                }
            });
        }
    });
});
