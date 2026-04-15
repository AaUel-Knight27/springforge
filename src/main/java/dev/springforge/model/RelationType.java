package dev.springforge.model;

/**
 * JPA relationship types.
 */
public enum RelationType {
    ONE_TO_ONE("@OneToOne"),
    ONE_TO_MANY("@OneToMany"),
    MANY_TO_ONE("@ManyToOne"),
    MANY_TO_MANY("@ManyToMany");

    private final String annotation;

    RelationType(String annotation) {
        this.annotation = annotation;
    }

    public String getAnnotation() {
        return annotation;
    }

    public static RelationType fromString(String input) {
        return switch (input.toLowerCase().replace("_", "-")) {
            case "one-to-one", "onetoone" -> ONE_TO_ONE;
            case "one-to-many", "onetomany" -> ONE_TO_MANY;
            case "many-to-one", "manytoone" -> MANY_TO_ONE;
            case "many-to-many", "manytomany" -> MANY_TO_MANY;
            default -> throw new IllegalArgumentException("Unknown relation type: " + input
                    + ". Use: one-to-one, one-to-many, many-to-one, many-to-many");
        };
    }
}
