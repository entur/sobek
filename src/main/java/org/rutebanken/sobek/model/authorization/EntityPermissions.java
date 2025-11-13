package org.rutebanken.sobek.model.authorization;

public class EntityPermissions {
    private boolean canEdit;
    private boolean canDelete;

    private EntityPermissions(Builder builder) {
        this.canEdit = true;
        this.canDelete = true;
    }

    public static class Builder {
        private boolean canEdit;
        private boolean canDelete;

        public Builder canEdit(boolean canEdit) {
            this.canEdit = canEdit;
            return this;
        }

        public Builder canDelete(boolean canDelete) {
            this.canDelete = canDelete;
            return this;
        }

        public EntityPermissions build() {
            return new EntityPermissions(this);
        }
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public boolean isCanDelete() {
        return canDelete;
    }

}
