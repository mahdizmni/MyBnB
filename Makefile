all :
	@mysql -u root -p'Mz2468!0' MyBnB< setup/init.sql && echo tables added.
	@mysql -u root -p'Mz2468!0' MyBnB< setup/sample_user.sql && echo User data loaded.
	@mysql -u root -p'Mz2468!0' MyBnB< setup/listings.sql && echo Listings data loaded.
	@mysql -u root -p'Mz2468!0' MyBnB< setup/type.sql && echo Type data loaded.
	@mysql -u root -p'Mz2468!0' MyBnB< setup/listingstype.sql && echo ListingsType data loaded.
	@mysql -u root -p'Mz2468!0' MyBnB< setup/owns.sql && echo Owns data loaded.
	@mysql -u root -p'Mz2468!0' MyBnB< setup/period.sql && echo Period data loaded.
	@mysql -u root -p'Mz2468!0' MyBnB< setup/availablein.sql && echo AvailableIn data loaded.
	@mysql -u root -p'Mz2468!0' MyBnB< setup/books.sql && echo Books data loaded.
	@mysql -u root -p'Mz2468!0' MyBnB< setup/amenities.sql && echo Amenities data loaded.
	@mysql -u root -p'Mz2468!0' MyBnB< setup/has.sql && echo Has data loaded.
	@mysql -u root -p'Mz2468!0' MyBnB< setup/city.sql && echo City data loaded.
	@mysql -u root -p'Mz2468!0' MyBnB< setup/country.sql && echo Country data loaded.
	@mysql -u root -p'Mz2468!0' MyBnB< setup/belongsto.sql && echo BelongsTo data loaded.
	@mysql -u root -p'Mz2468!0' MyBnB< setup/address.sql && echo Address data loaded.
	@mysql -u root -p'Mz2468!0' MyBnB< setup/residesin.sql && echo ResidesIn data loaded.
	@mysql -u root -p'Mz2468!0' MyBnB< setup/isin.sql && echo IsIn data loaded.
	@mysql -u root -p'Mz2468!0' MyBnB< setup/locatedin.sql && echo LocatedIn data loaded.