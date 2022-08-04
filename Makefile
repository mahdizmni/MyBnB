all :
	@mysql -uroot -p MyBnB < setup/init.sql && echo tables added.
	@mysql -uroot -p MyBnB < setup/sample_user.sql && echo User data loaded.
	@mysql -uroot -p MyBnB < setup/listings.sql && echo Listings data loaded.
	@mysql -uroot -p MyBnB < setup/type.sql && echo Type data loaded.
	@mysql -uroot -p MyBnB < setup/listingstype.sql && echo ListingsType data loaded.
	@mysql -uroot -p MyBnB < setup/owns.sql && echo Owns data loaded.
	@mysql -uroot -p MyBnB < setup/period.sql && echo Period data loaded.
	@mysql -uroot -p MyBnB < setup/availablein.sql && echo AvailableIn data loaded.
	@mysql -uroot -p MyBnB < setup/books.sql && echo Books data loaded.
	@mysql -uroot -p MyBnB < setup/amenities.sql && echo Amenities data loaded.
	@mysql -uroot -p MyBnB < setup/has.sql && echo Has data loaded.
	@mysql -uroot -p MyBnB < setup/city.sql && echo City data loaded.
	@mysql -uroot -p MyBnB < setup/country.sql && echo Country data loaded.
	@mysql -uroot -p MyBnB < setup/belongsto.sql && echo BelongsTo data loaded.
	@mysql -uroot -p MyBnB < setup/address.sql && echo Address data loaded.
	@mysql -uroot -p MyBnB < setup/residesin.sql && echo ResidesIn data loaded.
	@mysql -uroot -p MyBnB < setup/isin.sql && echo IsIn data loaded.
	@mysql -uroot -p MyBnB < setup/locatedin.sql && echo LocatedIn data loaded.