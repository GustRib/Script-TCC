package service;

public enum ConflictStatus {
    DD("Exclusão dupla"),
    AU("Adicionado por nós, modificado pelo outro"),
    UD("Modificado por nós, excluído pelo outro"),
    UA("Modificado pelo outro, adicionado por nós"),
    DU("Excluído por nós, modificado pelo outro"),
    AA("Adicionado por ambos"),
    UU("Modificado por ambos"),
    DEFAULT("Desconhecido");

    private final String description;

    ConflictStatus(String description) {
        this.description = description;
    }

    public static String fromCode(String code) {
        for (ConflictStatus status : values()) {
            if (status.name().equals(code)) {
                return status.description;
            }
        }
        return DEFAULT.description;
    }
}
