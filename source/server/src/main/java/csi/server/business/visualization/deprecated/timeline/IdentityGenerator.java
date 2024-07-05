package csi.server.business.visualization.deprecated.timeline;

public interface IdentityGenerator<Identifier, Data> {

    Identifier generate(Data data, int row);
}
