package net.sourceforge.ondex.core.persistent;

import net.sourceforge.ondex.core.Unit;
import net.sourceforge.ondex.core.base.UnitImpl;
import net.sourceforge.ondex.core.util.UpdateListener;

public class BerkeleyUnit extends UnitImpl implements Updatable,
		BerkeleySerializable {

	public static final Persistence.MetaDataFactory<BerkeleyUnit, Unit> FACTORY = new Persistence.MetaDataFactory<BerkeleyUnit, Unit>(
			"[Unit - deserialize]") {
		@Override
		public BerkeleyUnit create(long sid, String id, String fullname,
				String description) {
			return new BerkeleyUnit(sid, id, fullname, description);
		}

		@Override
		BerkeleyUnit convert(Unit old) {
			return new BerkeleyUnit(old.getSID(), old.getId(),
					old.getFullname(), old.getDescription());
		}
	};

	private UpdateListener l;

	public BerkeleyUnit(long sid, String id, String fullname, String description) {
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
