
DROP TRIGGER IF EXISTS t_graphstorage_graph_info ON public.graphstorages;

DROP TABLE IF EXISTS public.graphstorages;

CREATE TABLE public.graphstorages
(
  visualizations_uuid character varying(255) NOT NULL,
  graph_info oid NOT NULL,
  CONSTRAINT graphstorages_pkey PRIMARY KEY (visualizations_uuid)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.graphstorages
  OWNER TO csiserver;

CREATE TRIGGER t_graphstorage_graph_info
  BEFORE UPDATE OR DELETE
  ON public.graphstorages
  FOR EACH ROW
  EXECUTE PROCEDURE public.lo_manage('graph_info');

