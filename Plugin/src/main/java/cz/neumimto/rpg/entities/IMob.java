package cz.neumimto.rpg.entities;

import org.spongepowered.api.entity.living.Living;

/**
 * Created by NeumimTo on 19.12.2015.
 */
public interface IMob<T extends Living> extends IEntity {

	double getExperiences();

	void setExperiences(double exp);

	@Override
	default IEntityType getType() {
		return IEntityType.MOB;
	}

	void attach(T t);

	void detach();

}
