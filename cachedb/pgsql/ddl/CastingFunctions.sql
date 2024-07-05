
	-- From String data type to any other

create or replace function cast_string(input text) returns text as $$
	begin
		return cast(input as text);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_boolean(input text) returns boolean as $$
	begin
		return cast(input as boolean);
	exception
		when data_exception then
			return null;
		when zero_length_character_string then
			return null;
		when invalid_text_representation then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_integer(input text) returns bigint as $$
	begin
		return cast(input as bigint);
	exception
		when data_exception then
			return null;
		when numeric_value_out_of_range then
			return null;
		when zero_length_character_string then
			return null;
		when invalid_text_representation then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_double(input text) returns float8 as $$
	begin
		return cast(input as float8);
	exception
		when data_exception then
			return null;
		when numeric_value_out_of_range then
			return null;
		when zero_length_character_string then
			return null;
		when invalid_text_representation then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_datetime(input text) returns timestamp without time zone as $$
	begin
		return cast(input as timestamp);
	exception
		when data_exception then
			return null;
		when datetime_field_overflow then
			return null;
		when invalid_datetime_format then
			return null;
		when zero_length_character_string then
			return null;
		when invalid_text_representation then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_date(input text) returns date as $$
	begin
		return cast(input as date);
	exception
		when data_exception then
			return null;
		when invalid_datetime_format then
			return null;
		when zero_length_character_string then
			return null;
		when invalid_text_representation then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_time(input text) returns time without time zone as $$
	begin
		return cast(input as time);
	exception
		when data_exception then
			return null;
		when invalid_datetime_format then
			return null;
		when zero_length_character_string then
			return null;
		when invalid_text_representation then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_interval(input text) returns interval as $$
	begin
		return cast(input as interval);
	exception
		when data_exception then
			return null;
		when invalid_datetime_format then
			return null;
		when zero_length_character_string then
			return null;
		when interval_field_overflow then
			return null;
		when invalid_text_representation then
			return null;
	end;
	$$ language plpgsql immutable;

	-- From Number data type to any other

create or replace function cast_string(input float8) returns text as $$
	begin
		return cast(input as text);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_boolean(input float8) returns boolean as $$
	begin
		return cast(input as boolean);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_integer(input float8) returns bigint as $$
	begin
		return cast(input as bigint);
	exception
		when data_exception then
			return null;
		when numeric_value_out_of_range then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_double(input float8) returns float8 as $$
	begin
		return cast(input as float8);
	exception
		when data_exception then
			return null;
		when numeric_value_out_of_range then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_datetime(input float8) returns timestamp without time zone as $$
	begin
		return cast(input as timestamp);
	exception
		when data_exception then
			return null;
		when datetime_field_overflow then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_date(input float8) returns date as $$
	begin
		return cast(input as date);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_time(input float8) returns time without time zone as $$
	begin
		return cast(input as time);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_interval(input float8) returns interval as $$
	begin
		return cast(input as interval);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;

	-- From Integer data type to any other

create or replace function cast_string(input bigint) returns text as $$
	begin
		return cast(input as text);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_boolean(input bigint) returns boolean as $$
	begin
		return cast(input as boolean);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_integer(input bigint) returns bigint as $$
	begin
		return cast(input as bigint);
	exception
		when data_exception then
			return null;
		when numeric_value_out_of_range then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_double(input bigint) returns float8 as $$
	begin
		return cast(input as float8);
	exception
		when data_exception then
			return null;
		when numeric_value_out_of_range then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_datetime(input bigint) returns timestamp without time zone as $$
	begin
		return cast(input as timestamp);
	exception
		when data_exception then
			return null;
		when datetime_field_overflow then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_date(input bigint) returns date as $$
	begin
		return cast(input as date);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_time(input bigint) returns time without time zone as $$
	begin
		return cast(input as time);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_interval(input bigint) returns interval as $$
	begin
		return cast(input as interval);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;

	-- From DateTime (without timezone) data type to any other

create or replace function cast_string(input timestamp without time zone) returns text as $$
	begin
		return cast(input as text);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_boolean(input timestamp without time zone) returns boolean as $$
	begin
		return cast(input as boolean);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_integer(input timestamp without time zone) returns bigint as $$
	begin
		return cast(input as bigint);
	exception
		when data_exception then
			return null;
		when numeric_value_out_of_range then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_double(input timestamp without time zone) returns float8 as $$
	begin
		return cast(input as float8);
	exception
		when data_exception then
			return null;
		when numeric_value_out_of_range then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_datetime(input timestamp without time zone) returns timestamp without time zone as $$
	begin
		return cast(input as timestamp);
	exception
		when data_exception then
			return null;
		when datetime_field_overflow then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_date(input timestamp without time zone) returns date as $$
	begin
		return cast(input as date);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_time(input timestamp without time zone) returns time without time zone as $$
	begin
		return cast(input as time);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_interval(input timestamp without time zone) returns interval as $$
	begin
		return cast(input as interval);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;

	-- From DateTime (with timezone) data type to any other

create or replace function cast_string(input timestamp with time zone) returns text as $$
	begin
		return cast(input as text);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_boolean(input timestamp with time zone) returns boolean as $$
	begin
		return cast(input as boolean);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_integer(input timestamp with time zone) returns bigint as $$
	begin
		return cast(input as bigint);
	exception
		when data_exception then
			return null;
		when numeric_value_out_of_range then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_double(input timestamp with time zone) returns float8 as $$
	begin
		return cast(input as float8);
	exception
		when data_exception then
			return null;
		when numeric_value_out_of_range then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_datetime(input timestamp with time zone) returns timestamp without time zone as $$
	begin
		return cast(input as timestamp);
	exception
		when data_exception then
			return null;
		when datetime_field_overflow then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_date(input timestamp with time zone) returns date as $$
	begin
		return cast(input as date);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_time(input timestamp with time zone) returns time without time zone as $$
	begin
		return cast(input as time);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_interval(input timestamp with time zone) returns interval as $$
	begin
		return cast(input as interval);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;

	-- From Date data type to any other

create or replace function cast_string(input date) returns text as $$
	begin
		return cast(input as text);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_boolean(input date) returns boolean as $$
	begin
		return cast(input as boolean);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_integer(input date) returns bigint as $$
	begin
		return cast(input as bigint);
	exception
		when data_exception then
			return null;
		when numeric_value_out_of_range then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_double(input date) returns float8 as $$
	begin
		return cast(input as float8);
	exception
		when data_exception then
			return null;
		when numeric_value_out_of_range then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_datetime(input date) returns timestamp without time zone as $$
	begin
		return cast(input as timestamp);
	exception
		when data_exception then
			return null;
		when datetime_field_overflow then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_date(input date) returns date as $$
	begin
		return cast(input as date);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_time(input date) returns time without time zone as $$
	begin
		return cast(input as time);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_interval(input date) returns interval as $$
	begin
		return cast(input as interval);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;

	-- From Time (without timezone) data type to any other

create or replace function cast_string(input time without time zone) returns text as $$
	begin
		return cast(input as text);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_boolean(input time without time zone) returns boolean as $$
	begin
		return cast(input as boolean);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_integer(input time without time zone) returns bigint as $$
	begin
		return cast(input as bigint);
	exception
		when data_exception then
			return null;
		when numeric_value_out_of_range then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_double(input time without time zone) returns float8 as $$
	begin
		return cast(input as float8);
	exception
		when data_exception then
			return null;
		when numeric_value_out_of_range then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_datetime(input time without time zone) returns timestamp without time zone as $$
	begin
		return cast(input as timestamp);
	exception
		when data_exception then
			return null;
		when datetime_field_overflow then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_date(input time without time zone) returns date as $$
	begin
		return cast(input as date);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_time(input time without time zone) returns time without time zone as $$
	begin
		return cast(input as time);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_interval(input time without time zone) returns interval as $$
	begin
		return cast(input as interval);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;

	-- From Time (with timezone) data type to any other

create or replace function cast_string(input time with time zone) returns text as $$
	begin
		return cast(input as text);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_boolean(input time with time zone) returns boolean as $$
	begin
		return cast(input as boolean);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_integer(input time with time zone) returns bigint as $$
	begin
		return cast(input as bigint);
	exception
		when data_exception then
			return null;
		when numeric_value_out_of_range then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_double(input time with time zone) returns float8 as $$
	begin
		return cast(input as float8);
	exception
		when data_exception then
			return null;
		when numeric_value_out_of_range then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_datetime(input time with time zone) returns timestamp without time zone as $$
	begin
		return cast(input as timestamp);
	exception
		when data_exception then
			return null;
		when datetime_field_overflow then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_date(input time with time zone) returns date as $$
	begin
		return cast(input as date);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_time(input time with time zone) returns time without time zone as $$
	begin
		return cast(input as time);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_interval(input time with time zone) returns interval as $$
	begin
		return cast(input as interval);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;

	-- From Interval data type to any other

create or replace function cast_string(input interval) returns text as $$
	begin
		return cast(input as text);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_boolean(input interval) returns boolean as $$
	begin
		return cast(input as boolean);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_integer(input interval) returns bigint as $$
	begin
		return cast(input as bigint);
	exception
		when data_exception then
			return null;
		when numeric_value_out_of_range then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_double(input interval) returns float8 as $$
	begin
		return cast(input as float8);
	exception
		when data_exception then
			return null;
		when numeric_value_out_of_range then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_datetime(input interval) returns timestamp without time zone as $$
	begin
		return cast(input as timestamp);
	exception
		when data_exception then
			return null;
		when datetime_field_overflow then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_date(input interval) returns date as $$
	begin
		return cast(input as date);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_time(input interval) returns time without time zone as $$
	begin
		return cast(input as time);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_interval(input interval) returns interval as $$
	begin
		return cast(input as interval);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_boolean(input boolean) returns boolean as $$
	begin
		return cast(input as boolean);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_boolean(input text) returns boolean as $$
	begin
		return cast(input as text);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;
create or replace function cast_string(input boolean) returns text as $$
	begin
		return cast(input as boolean);
	exception
		when data_exception then
			return null;
	end;
	$$ language plpgsql immutable;
