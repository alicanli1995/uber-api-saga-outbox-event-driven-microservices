create table if not exists driver.customer_request_outbox
(
    id            uuid    not null,
    created_at    timestamp,
    driver_status varchar(255),
    outbox_status varchar(255),
    payload       varchar(2500),
    processed_at  timestamp,
    saga_id       uuid,
    type          varchar(255),
    version       integer not null,
    primary key (id)
);

alter table driver.customer_request_outbox
    owner to postgres;

create table if not exists driver.drivers
(
    id                         varchar(255) not null,
    driver_status              varchar(255),
    email                      varchar(255) not null,
    ip_address                 varchar(255),
    name                       varchar(255),
    phone                      varchar(255),
    locations_id               uuid,
    pending_request_request_id uuid,
    primary key (id),
    constraint uk_re66mdta4hy6pxm2w1rqu08jv
        unique (email),
    constraint fkcch8pqj05f38nuhf5lwom2u7k
        foreign key (locations_id) references public.locations,
    constraint fkmm7uuxb1emh0hj3wl4mr3m3ql
        foreign key (pending_request_request_id) references public.pending_requests
);

alter table driver.drivers
    owner to postgres;

