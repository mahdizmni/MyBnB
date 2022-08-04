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