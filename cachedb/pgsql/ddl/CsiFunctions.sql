create or replace function num_token(arg1 text, arg2 text) returns integer as $$
	begin
		return array_length(string_to_array(arg1, arg2), 1);
	exception
		when others then
			return null;
	end;
	$$ language plpgsql immutable;

create or replace function get_last_token(arg1 text, arg2 text) returns text as $$
	declare
		myArray text[] = string_to_array(arg1, arg2);
		myCount  bigint = array_length(myArray, 1)::bigint;
	begin
		return myArray[myCount];
	exception
		when others then
			return null;
	end;
	$$ language plpgsql immutable;

create or replace function get_nth_token(arg1 text, arg2 text, arg3 bigint) returns text as $$
	declare
		myArray text[] = string_to_array(arg1, arg2);
		myCount  bigint = array_length(myArray, 1)::bigint;
	begin
		if arg3 < 0 and arg3 + myCount >= 0 then
			return myArray[myCount + arg3 + 1];
		elsif arg3 > 0 and arg3 <= myCount then
			return myArray[arg3];
		else
			return null;
		end if;
	exception
		when others then
			return null;
	end;
	$$ language plpgsql immutable;

create or replace function extract_value(myInput char[], myStart bigint, doExponent boolean)
		returns double precision as
	$$
	declare
		myDividend double precision;
		myDivisor double precision;
		myLimit bigint;
		myCount bigint;
		myValue bigint;
		myNext bigint;
		myExponent bigint;
		myMinus boolean;
	begin
		myMinus = false;
		myDividend = null;
		myDivisor = null;
		myExponent = null;
		myLimit := array_length(myInput, 1);
		myCount := myStart;
		while (myCount <= myLimit) loop
			myValue = ascii(myInput[myCount]);
			if ((48 <= myValue) and (57 >= myValue)) then
				if (myDividend is null) then
					myDividend = 0;
				end if;
				myDividend = (myDividend * 10) + myValue - 48;
				if (myDivisor is not null) then
					myDivisor = myDivisor * 10;
				end if;
			elsif (43 = myValue) then
				if (myDividend is null) then
					if (myCount < myLimit) then
						myNext = ascii(myInput[myCount+1]);
						if ((48 <= myNext) and (57 >= myNext)) then
							myDividend = 0;
						end if;
					end if;
				else
					exit;
				end if;
			elsif (45 = myValue) then
				if (myDividend is null) then
					if (myCount < myLimit) then
						myNext = ascii(myInput[myCount+1]);
						if ((48 <= myNext) and (57 >= myNext)) then
							myDividend = 0;
							myMinus = true;
						end if;
					end if;
				else
					exit;
				end if;
			elsif (46 = myValue) then
				if (myDividend is not null) then
					if (myDivisor is null) then
						myDivisor = 1;
					else
						exit;
					end if;
				else
					if (myCount < myLimit) then
						myNext = ascii(myInput[myCount+1]);
						if ((48 <= myNext) and (57 >= myNext)) then
							myDividend = 0;
							myDivisor = 1;
						end if;
					end if;
				end if;
			elsif ((doExponent is true) and ((69 = myValue) or (101 = myValue))) then
				if (myDividend is not null) then
					if (myCount < myLimit) then
						myNext = ascii(myInput[myCount+1]);
						if ((43 = myNext) or (45 = myNext) or (46 = myNext)) then
							if ((myCount + 1) < myLimit) then
								myNext = ascii(myInput[myCount+2]);
							end if;
						end if;
						if ((48 <= myNext) and (57 >= myNext)) then
							myNext = myCount + 1;
							myExponent = extract_value(myInput, myNext, false);
						end if;
					end if;
					exit;
				end if;
			elsif (myDividend is not null) then
				exit;
			end if;
			myCount = myCount + 1;
		end loop;
		if (myDividend is not null) then
			if (myDivisor is not null) then
				myDividend = myDividend / myDivisor;
			end if;
			if (myMinus) then
				myDividend = -myDividend;
			end if;
			if (myExponent is not null) then
				myDividend = myDividend * (10 ^ myExponent);
			end if;
		end if;
		return myDividend;
	end;
	$$
	language 'plpgsql' immutable;

create or replace function extract_value(myInput char[], myStart bigint)
		returns double precision as
	$$
	declare
		myDividend double precision;
	begin
		myDividend = extract_value(myInput, myStart, true);
		return myDividend;
	end;
	$$
	language plpgsql immutable;

create or replace function skip_value(myInput char[], myStart bigint, doExponent boolean)
		returns bigint as
	$$
	declare
		myDividend boolean;
		myDivisor boolean;
		myLimit bigint;
		myCount bigint;
		myValue bigint;
		myNext bigint;
		myMinus boolean;
	begin
		myMinus = false;
		myDividend = false;
		myDivisor = false;
		myLimit := array_length(myInput, 1);
		myCount := myStart;
		while (myCount <= myLimit) loop
			myValue = ascii(myInput[myCount]);
			if ((48 <= myValue) and (57 >= myValue)) then
				if (myDividend is false) then
					myDividend = true;
				end if;
			elsif (43 = myValue) then
				if (myDividend is false) then
					if (myCount < myLimit) then
						myNext = ascii(myInput[myCount+1]);
						if ((48 <= myNext) and (57 >= myNext)) then
							myDividend = true;
						end if;
					end if;
				else
					exit;
				end if;
			elsif (45 = myValue) then
				if (myDividend is false) then
					if (myCount < myLimit) then
						myNext = ascii(myInput[myCount+1]);
						if ((48 <= myNext) and (57 >= myNext)) then
							myDividend = true;
						end if;
					end if;
				else
					exit;
				end if;
			elsif (46 = myValue) then
				if (myDividend is true) then
					if (myDivisor is false) then
						myDivisor = true;
					else
						exit;
					end if;
				else
					if (myCount < myLimit) then
						myNext = ascii(myInput[myCount+1]);
						if ((48 <= myNext) and (57 >= myNext)) then
							myDividend = true;
							myDivisor = true;
						end if;
					end if;
				end if;
			elsif ((doExponent is true) and ((69 = myValue) or (101 = myValue))) then
				if (myDividend is not null) then
					if (myCount < myLimit) then
						myNext = ascii(myInput[myCount+1]);
						if ((43 = myNext) or (45 = myNext) or (46 = myNext)) then
							if ((myCount + 1) < myLimit) then
								myNext = ascii(myInput[myCount+2]);
							end if;
						end if;
						if ((48 <= myNext) and (57 >= myNext)) then
							myNext = myCount + 1;
							myCount = myCount + skip_value(myInput, myNext, false);
						end if;
					end if;
					if (myExponent is not null) then
					end if;
					exit;
				end if;
			elsif (myDividend is true) then
				exit;
			end if;
			myCount = myCount + 1;
		end loop;
		return myCount;
	end;
	$$
	language 'plpgsql' immutable;

create or replace function skip_value(myInput char[], myStart bigint)
		returns bigint as
	$$
	declare
		mySkipCount bigint;
	begin
		mySkipCount = skip_value(myInput, myStart, true);
		return mySkipCount;
	end;
	$$
	language 'plpgsql' immutable;

create or replace function extract_nth_value(myInput char[], myIndex bigint)
		returns double precision as
	$$
	declare
		mySize bigint;
		myLimit bigint;
		myOffset bigint;
		myCount bigint;
		myValueSet bigint[];
	begin
		myLimit := array_length(myInput, 1);
		if (0 < myIndex) then
			myCount = 1;
			myOffset = 1;
			while ((myCount < myIndex) and (myOffset <= myLimit)) loop
				myOffset = skip_value(myInput, myOffset);
				myCount = myCount + 1;
			end loop;
			if ((myCount = myIndex) and (myOffset <= myLimit)) then
				return extract_value(myInput, myOffset);
			else
				return null;
			end if;
		elsif (0 > myIndex) then
			myInput[myLimit+1] = 'x';
			myInput[myLimit+2] = 'x';
			myLimit = myLimit + 2;
			raise notice 'Buffer size = %', myLimit;
			raise notice 'BUFFER: %', myInput;
			mySize = 2 - myIndex;
			for i in 1..mySize loop
				myValueSet[i] = 0;
			end loop;
			myValueSet[mySize] = 1;
			myOffset = 1;
			while (myOffset <= myLimit) loop
				myOffset = skip_value(myInput, myOffset);
				for i in 2..mySize loop
					raise notice 'myValueSet[%] = %', (i-1), myValueSet[i];
					myValueSet[i-1] = myValueSet[i];
				end loop;
				raise notice 'myValueSet[%] = %', mySize, myOffset;
				myValueSet[mySize] = myOffset;
			end loop;
			myOffset = myValueSet[1];
			if (0 < myOffset) then
				return extract_value(myInput, myOffset);
			else
				return null;
			end if;
		else
			return null;
		end if;
	end;
	$$
	language 'plpgsql' immutable;

create or replace function csi_asin(myInput double precision)
		returns double precision as
	$$
	begin
		if ((-1.0 <= myInput) and (1.0 >= myInput)) then
			return asin(myInput);
		else
			return null;
		end if;
	end;
	$$
	language 'plpgsql' immutable;

create or replace function csi_acos(myInput double precision)
		returns double precision as
	$$
	declare
		myValue double precision;
	begin
		if ((-1.0 <= myInput) and (1.0 >= myInput)) then
			return acos(myInput);
		else
			return null;
		end if;
	end;
	$$
	language 'plpgsql' immutable;

create or replace function csi_atan(myInput double precision)
		returns double precision as
	$$
	begin
		return atan(myInput);
	end;
	$$
	language 'plpgsql' immutable;

create or replace function csi_acot(myInput double precision)
		returns double precision as
	$$
	declare
		myValue double precision;
	begin
		if (0.0 = myInput) then
			return acos(0.0);
		else
			myValue = 1.0 / myInput;
			return atan(myValue);
		end if;
	end;
	$$
	language 'plpgsql' immutable;

create or replace function time_from_timestamp(myInput timestamp)
		returns time as
	$$
	begin
		if (myInput is not null) then
			return myInput - date_trunc('day', myInput);
		else 
			return null;
		end if;
	end;
	$$
	language 'plpgsql' immutable;

create or replace function safe_factorial(input bigint) returns bigint as $$
	begin
		if ((-1 < input) and (21 > input)) then
			return input!;
		else
			return null;
		end if;
	end;
	$$ language plpgsql immutable;
