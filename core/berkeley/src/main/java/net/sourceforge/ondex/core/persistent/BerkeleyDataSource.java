package net.sourceforge.ondex.core.persistent;

import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.base.DataSourceImpl;
import net.sourceforge.ondex.core.util.UpdateListener;

public class BerkeleyDataSource extends DataSourceImpl implements Updatable,
		BerkeleySerializable {
	public static final Persistence.MetaDataFactory<BerkeleyDataSource, DataSource> FACTORY = new Persistence.MetaDataFactory<BerkeleyDataSource, DataSource>(
			"[DataSource - deserialize]") {
		@Override
		public BerkeleyDataSource create(long sid, String id, String fullname,
				String description) {
			return new BerkeleyDataSource(sid, id, fullname, description);
		}

		@Override
		BerkeleyDataSource convert(DataSource old) {
			return new BerkeleyDataSource(old.getSID(), old.getId(),
					old.getFullname(), old.getDescription());
		}
	};

	private UpdateListener l;

	public BerkeleyDataSource(long sid, String id, String fullname,
			String description) {
		super(sid, id, fullname, description);
	}

	@Override
	public byte[] serialise() {
		return serialise(getSID());
	}

	@Override
	public byte[] serialise(long sid) {
		return SerialiseMetaDataFactory.serialise(sid, this);
	}

	@Override
	public void fireUpdateEvent() {
		if (l != null)
			l.performUpdate(this);
	}

	@Override
	public void setUpdateListener(UpdateListener l) {
		this.l = l;
	}

	@Override
	public void setFullname(String s) {
		super.setFullname(s);
		fireUpdateEvent();
	}

	@Override
	public void setDescription(String s) {
		super.setDescription(s);
		fireUpdateEvent();
	}

}
